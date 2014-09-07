/**
 *  VOTSTACK.C -- (Internal) Methods to manage the parser Element stack.
 *
 *  @file       votStack.c
 *  @author     Mike Fitzpatrick and Eric Timmermann
 *  @date       8/03/09
 *
 *  @brief      (Internal) Methods to manage the parser Element stack.
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <strings.h>

#include "votParseP.h"


/** 
 *  vot_newStack -- Makes a new stack (internal method)
 * 
 *  @brief  Makes a new stack (internal method)
 *  @fn     Stack *vot_newStack (void)
 *
 *  @return 		A pointer to a new Stack.
 */
Stack *
vot_newStack (void)
{
    return ( (Stack *) calloc (1, sizeof (Stack)) );
}


/** 
 *  votPop -- Return a Node from the top of the stack (internal method)
 *
 *  @brief  Return a Node from the top of the stack (internal method)
 *  @fn     Element *votPop (Stack *st)
 *
 *  @param  st 		A pointer to a Stack
 *  @return 		A pointer to the popped Element.
 */
Element *
votPop (Stack *st)
{
    Node    *old;
    Element *elem;

    st->level--;
    if (vot_isEmpty (st))
        return (NULL);
    else {
        old = st->head;
        st->head = (Node *) old->next;
    }

    elem = old->element;
    free (old);

    return (elem);
}


/** 
 *  votPush -- Push a Node to the top of the stack (internal method)
 *
 *  @brief  Push a Node to the top of the stack (internal method)
 *  @fn     votPush (Stack *st, Element *elem)
 *
 *  @param  st 	    	A pointer to a Stack
 *  @param  elem    	A pointer to an element to be put on the stack
 *  @return 	    	nothing
 */
void
votPush (Stack *st, Element *elem)
{
    Node *new = (Node *) calloc (1, sizeof (Node));

    st->level++;
    new->element = elem;

    if (vot_isEmpty (st)) {
        st->head = new;
        new->next = NULL;
    } else {
        new->next = (Node *) st->head;
        st->head = new;
    }
}


/** 
 *  votPeek -- Peek at Element on top of the Stack (internal method)
 *
 *  @brief  Peek at Element on top of the Stack (internal method)
 *  @fn     Element *votPeek (Stack *st)
 *
 *  @param  st 		A pointer to a Stack
 *  @return 		A pointer to the head Element, or NULL if empty
 */
Element *
votPeek (Stack *st)
{
    if (!vot_isEmpty (st))
        return (st->head->element);
    else
        return (NULL);
}


/** 
 *  vot_isEmpty -- Checks to see if the stack is empty (internal method)
 *
 *  @brief  Checks to see if the stack is empty (internal method)
 *  @fn     int vot_isEmpty (Stack *st)
 *
 *  @param  st 	A pointer to a Stack
 *  @return 	\a 1 if true, \a 0 if false.
 */
int
vot_isEmpty (Stack *st) { return (!st->head); }


/** 
 *  vot_clearStack -- Clear the stack (internal method)
 *
 *  @brief  Clear the stack (internal method)
 *  @fn     vot_clearStack (Stack *st)
 *
 *  @param  st 		A pointer to a Stack
 *  @return 		nothing
 */
void
vot_clearStack (Stack *st)
{
    while (!vot_isEmpty (st))
        votPop (st);
}


/** 
 *  vot_printStack -- Print the name of all the stack elements (internal method)
 *
 *  @brief  Print the name of all the stack elements (internal method)
 *  @fn     vot_printStack (Stack *st)
 *
 *  @param  st 		A pointer to a Stack
 *  @return 		nothing
 */
void
vot_printStack (Stack *st)
{
    Node   *cur;

    for (cur=st->head; cur != NULL; cur = cur->next)
        printf ("%s\n", vot_elemName (cur->element));
}
